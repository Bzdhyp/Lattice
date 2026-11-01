#!/usr/bin/env python3
"""Static checks for the single Lattice universal jar.

The release contract is intentionally strict: build/dist must contain exactly
one jar, and that jar must carry both loader metadata files and both
Minecraft-version payload trees.
"""

from __future__ import annotations

import json
import struct
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIST = ROOT / "build" / "dist"
ARTIFACT = "lattice-0.1.0-universal.jar"

FABRIC_RANGE = ">=1.21.11 <26.4"
NEOFORGE_RANGE = "[1.21.11,26.4)"
EXPECTED_SHARED = {
    "dev/lattice/common/Lattice.class",
    "dev/lattice/fabric/LatticeFabricEntry.class",
    "dev/lattice/neoforge/LatticeNeoForgeEntry.class",
}
EXPECTED_PAYLOADS = {
    "dev/lattice/v12111/MinecraftVersionAdapter.class",
    "dev/lattice/v12111/fabric/LatticeFabricPayload.class",
    "dev/lattice/v12111/neoforge/LatticeNeoForgePayload.class",
    "dev/lattice/v263/MinecraftVersionAdapter.class",
    "dev/lattice/v263/fabric/LatticeFabricPayload.class",
    "dev/lattice/v263/neoforge/LatticeNeoForgePayload.class",
}


def fail(message: str) -> None:
    print(f"FAIL: {message}")
    raise SystemExit(1)


def class_major(data: bytes) -> int:
    if len(data) < 8 or data[:4] != b"\xca\xfe\xba\xbe":
        fail("not a class file")
    return struct.unpack(">H", data[6:8])[0]


def main() -> int:
    if not DIST.is_dir():
        fail(f"missing build output directory: {DIST}")

    jars = sorted(DIST.glob("*.jar"))
    if [path.name for path in jars] != [ARTIFACT]:
        fail(
            "build/dist must contain exactly one release jar; found: "
            + ", ".join(path.name for path in jars)
        )

    path = DIST / ARTIFACT
    with zipfile.ZipFile(path) as jar:
        names = jar.namelist()
        if len(names) != len(set(names)):
            fail(f"{ARTIFACT}: duplicate entries")

        for required in ("fabric.mod.json", "META-INF/neoforge.mods.toml"):
            if required not in names:
                fail(f"{ARTIFACT}: missing {required}")

        fabric = json.loads(jar.read("fabric.mod.json"))
        if fabric.get("id") != "lattice":
            fail(f"{ARTIFACT}: fabric.mod.json id is not lattice")
        if fabric["entrypoints"]["main"] != ["dev.lattice.fabric.LatticeFabricEntry"]:
            fail(f"{ARTIFACT}: unexpected Fabric entrypoint")
        if fabric["depends"]["minecraft"] != FABRIC_RANGE:
            fail(f"{ARTIFACT}: Fabric range is {fabric['depends']['minecraft']!r}")
        if "[" in FABRIC_RANGE or "," in FABRIC_RANGE:
            fail(f"{ARTIFACT}: Fabric range is not a Fabric predicate")

        toml = jar.read("META-INF/neoforge.mods.toml").decode("utf-8")
        if 'modLoader = "javafml"' not in toml:
            fail(f"{ARTIFACT}: NeoForge modLoader missing")
        if 'modId = "lattice"' not in toml:
            fail(f"{ARTIFACT}: NeoForge modId missing")
        if f'versionRange = "{NEOFORGE_RANGE}"' not in toml:
            fail(f"{ARTIFACT}: NeoForge Minecraft range missing")
        if "[[dependencies.lattice]]" not in toml:
            fail(f"{ARTIFACT}: NeoForge dependency block missing")

        classes = {
            name: class_major(jar.read(name))
            for name in names
            if name.endswith(".class")
        }
        expected = EXPECTED_SHARED | EXPECTED_PAYLOADS
        missing = expected - classes.keys()
        if missing:
            fail(f"{ARTIFACT}: missing classes: {sorted(missing)}")

        if classes["dev/lattice/fabric/LatticeFabricEntry.class"] != 65:
            fail(f"{ARTIFACT}: shared Fabric entrypoint must be classfile 65")
        if classes["dev/lattice/neoforge/LatticeNeoForgeEntry.class"] != 65:
            fail(f"{ARTIFACT}: shared NeoForge entrypoint must be classfile 65")
        if classes["dev/lattice/v12111/fabric/LatticeFabricPayload.class"] != 65:
            fail(f"{ARTIFACT}: 1.21.11 payload must be classfile 65")
        if classes["dev/lattice/v263/fabric/LatticeFabricPayload.class"] != 69:
            fail(f"{ARTIFACT}: 26.3 payload must be classfile 69")

        for name in names:
            if name.startswith("META-INF/") and name.endswith((".SF", ".DSA", ".RSA")):
                fail(f"{ARTIFACT}: signature file in merged jar: {name}")

        print(
            f"PASS: {ARTIFACT} | entries={len(names)} | classes={len(classes)} "
            f"| majors={sorted(set(classes.values()))} | one jar only"
        )

    print("ALL CHECKS PASSED (single universal jar)")
    return 0


if __name__ == "__main__":
    sys.exit(main())

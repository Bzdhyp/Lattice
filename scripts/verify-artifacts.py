#!/usr/bin/env python3
"""Static checks for Lattice release jars.

This does not launch Minecraft. It verifies the things that are easy to break
while merging jars: duplicate entries, coexisting loader metadata, version
ranges, entrypoints and classfile versions.
"""

from __future__ import annotations

import json
import struct
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIST = ROOT / "build" / "dist"

ARTIFACTS = {
    "lattice-1.21.11-0.1.0-universal.jar": {
        "mc": "1.21.11",
        "fabric_range": ">=1.21.11 <1.21.12",
        "neoforge_range": "[1.21.11,1.21.12)",
        "classes": {65},
        "payload_versions": {"v12111"},
    },
    "lattice-26.3-0.1.0-universal.jar": {
        "mc": "26.3",
        "fabric_range": ">=26.3 <26.4",
        "neoforge_range": "[26.3,26.4)",
        "classes": {69},
        "payload_versions": {"v263"},
    },
    "lattice-1.21.11-26.3-0.1.0-universal.jar": {
        "mc": "1.21.11+26.3",
        "fabric_range": ">=1.21.11 <26.4",
        "neoforge_range": "[1.21.11,26.4)",
        "classes": {65, 69},
        "payload_versions": {"v12111", "v263"},
    },
}

SHARED_CLASSES = {
    "dev/lattice/core/Lattice.class",
    "dev/lattice/fabric/LatticeFabricEntry.class",
    "dev/lattice/neoforge/LatticeNeoForgeEntry.class",
}


def fail(message: str) -> None:
    print(f"FAIL: {message}")
    raise SystemExit(1)


def class_major(data: bytes) -> int:
    if len(data) < 8 or data[:4] != b"\xca\xfe\xba\xbe":
        fail("not a class file")
    return struct.unpack(">H", data[6:8])[0]


def check_jar(filename: str, spec: dict) -> None:
    path = DIST / filename
    if not path.is_file():
        fail(f"missing artifact {path}")

    with zipfile.ZipFile(path) as jar:
        names = jar.namelist()
        if len(names) != len(set(names)):
            fail(f"{filename}: duplicate entries")

        for required in ("fabric.mod.json", "META-INF/neoforge.mods.toml"):
            if required not in names:
                fail(f"{filename}: missing {required}")

        fabric = json.loads(jar.read("fabric.mod.json"))
        if fabric.get("id") != "lattice":
            fail(f"{filename}: fabric.mod.json id is not lattice")
        if fabric["entrypoints"]["main"] != ["dev.lattice.fabric.LatticeFabricEntry"]:
            fail(f"{filename}: unexpected Fabric entrypoint")
        actual_fabric_range = fabric["depends"]["minecraft"]
        if actual_fabric_range != spec["fabric_range"]:
            fail(
                f"{filename}: Fabric range {actual_fabric_range!r} "
                f"!= {spec['fabric_range']!r}"
            )
        if "[" in actual_fabric_range or "," in actual_fabric_range:
            fail(f"{filename}: Fabric range looks like a Maven range")

        toml = jar.read("META-INF/neoforge.mods.toml").decode("utf-8")
        if 'modLoader = "javafml"' not in toml:
            fail(f"{filename}: NeoForge modLoader missing")
        if 'modId = "lattice"' not in toml:
            fail(f"{filename}: NeoForge modId missing")
        if f'versionRange = "{spec["neoforge_range"]}"' not in toml:
            fail(f"{filename}: NeoForge Minecraft range missing")
        if "[[dependencies.lattice]]" not in toml:
            fail(f"{filename}: NeoForge dependency block missing")

        classes = {
            name: class_major(jar.read(name))
            for name in names
            if name.endswith(".class")
        }
        for required in SHARED_CLASSES:
            if required not in classes:
                fail(f"{filename}: missing {required}")

        if "dev/lattice/fabric/LatticeFabricEntry.class" not in classes:
            fail(f"{filename}: Fabric entrypoint class missing")
        if "dev/lattice/neoforge/LatticeNeoForgeEntry.class" not in classes:
            fail(f"{filename}: NeoForge entrypoint class missing")

        majors = set(classes.values())
        if majors != spec["classes"]:
            fail(f"{filename}: classfile majors {sorted(majors)} != {sorted(spec['classes'])}")

        for version in ("v12111", "v263"):
            present = any(f"dev/lattice/{version}/" in name for name in classes)
            if present and version not in spec["payload_versions"]:
                fail(f"{filename}: unexpected {version} payload")
            if not present and version in spec["payload_versions"]:
                fail(f"{filename}: missing {version} payload")

        for name in names:
            if name.startswith("META-INF/") and name.endswith((".SF", ".DSA", ".RSA")):
                fail(f"{filename}: signature file in merged jar: {name}")

        print(
            f"PASS: {filename} | entries={len(names)} | classes={len(classes)} "
            f"| majors={sorted(majors)} | fabric={actual_fabric_range}"
        )


def main() -> int:
    if not DIST.is_dir():
        fail(f"missing build output directory: {DIST}")
    for filename, spec in ARTIFACTS.items():
        check_jar(filename, spec)
    print(f"ALL CHECKS PASSED ({len(ARTIFACTS)} jars)")
    return 0


if __name__ == "__main__":
    sys.exit(main())

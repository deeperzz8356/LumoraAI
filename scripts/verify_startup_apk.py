"""Check actual DEX class definitions, not references, before installing an APK."""
import struct
import sys
import zipfile


def defined_classes(dex):
    def uint(offset):
        return struct.unpack_from('<I', dex, offset)[0]

    strings = []
    for index in range(uint(56)):
        offset = uint(uint(60) + index * 4)
        while dex[offset] & 0x80:
            offset += 1
        offset += 1
        strings.append(dex[offset:dex.index(b'\0', offset)].decode('utf-8', errors='replace'))
    types = [strings[uint(uint(68) + index * 4)] for index in range(uint(64))]
    return {types[uint(uint(100) + index * 32)] for index in range(uint(96))}


with zipfile.ZipFile(sys.argv[1]) as apk:
    classes = set()
    for name in apk.namelist():
        if name.startswith('classes') and name.endswith('.dex'):
            classes.update(defined_classes(apk.read(name)))

required = {
    'Lcom/deep/lumoraai/LumoraApplication;',
    'Lcom/deep/lumoraai/LumoraApplication_GeneratedInjector;',
    'Lcom/deep/lumoraai/Hilt_LumoraApplication;',
    'Lcom/deep/lumoraai/MainActivity;',
}
missing = required - classes
if missing:
    sys.exit('FAIL: Missing startup classes: ' + ', '.join(sorted(missing)))
print('PASS: All required startup classes are defined in the APK.')

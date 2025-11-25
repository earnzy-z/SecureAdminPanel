import sys
key = int(sys.argv[2], 16) if len(sys.argv) > 2 else 0x5A
b = sys.argv[1].encode("utf-8")
enc = bytes([x ^ key for x in b])
print('"{}"'.format("".join("\\x{:02x}".format(x) for x in enc)))
print("Key:", hex(key))
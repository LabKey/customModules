#!/usr/bin/python

import sys

def generate(prefix, suffix, bools):
    bitlen = len(bools)
    n = 1 << bitlen
    for i in range(0, n):
        sys.stdout.write(prefix)
        for j in range(0, bitlen):
            biton = (i & (1 << (bitlen - j - 1)))
            sys.stdout.write(bools[j])
            sys.stdout.write("+" if biton else "-")
        sys.stdout.write(suffix)
        sys.stdout.write("\n")

def main():
    script = sys.argv[0]

    if len(sys.argv) < 4:
        print "Usage: %s [--prefix prefix] [--suffix suffix] booleans" % script
        print
        print "Example: %s --prefix S/Exclude/14-/Lv/L/3+/4+/ --suffix :Count 107a 154 IFNg IL2 TNFa" % script
        print
        return -1

    prefix = ""
    suffix = ""

    q = 1
    if sys.argv[q] == "--prefix":
        prefix = sys.argv[q+1]
        q+=2

    if sys.argv[q] == "--suffix":
        suffix = sys.argv[q+1]
        q+=2

    booleans = sys.argv[q:]

    generate(prefix, suffix, booleans)

if __name__ == "__main__":
    main()

#!/usr/bin/python

#
# Copyright (c) 2013 LabKey Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

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

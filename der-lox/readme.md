# Der Lox

My C++ implementation of the Lox programming lanugage (with VM).
Maybe with some extra like static typing and null/nil safety.
I'll go with the white rabbit and who knows how deep the rabbit hole goes?

It is called Der Lox because CLox is for 'Pure C' implementation of the Lox programming language and
*cpp-lox*, *cc-lox*, *c-plus-lox* all sound not good enough for me.
Using German definite article made it sound decent.

## Compiling Der Lox
I use zig as a C++ build system because I choose to.

So to compile Der Lox the Zig 0.9+ should be installed and present in the path.

Then build is possible with the following command.

```bash
zig build
```

If everything goes well, good, ready-to-use executable will reside at `./zig-out/bin/DerLox[.exe]`

Have fun!

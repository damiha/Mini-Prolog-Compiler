
# Mini Prolog Compiler

This is my minimalistic implementation of the Prolog language.
The prolog compiler and virtual machine support by no means
everything that Prolog can do.

<p align="center">
<img src="img/LittleSokrates.webp" alt="drawing" width="300"/>
</p>

(This is how GPT4o imagines a mixture between Socrates and
the owl of [SWI Prolog](https://www.swi-prolog.org/) looks like)

## What is Prolog?

It's a logic programming language created in 1972. Prolog
is how artificial intelligence used to be done (expert systems).
You can state facts (your axioms) and inference rules
and then query the system.

The most famous example of them all:
```
% all humans are mortals (you have to read it from right to left)
mortal(X) :- human(X).

% socrates is a human
human(socrates).

% hence, socrates is mortal
?mortal(socrates).
```

Then the system responds:
```
VM: true

% after pressing 'ENTER'
VM: false
```

Why 'false' at the end? Whenever 'ENTER' is
pressed, backtracking is initiated. That means
the system tries to prove the query ```mortal(socrates)```
using a different rule than ```mortal(X) :- human(X).```
But there's no alternative way to come to that conclusion,
so the system says 'false'. 'False' doesn't mean that the
statement is untrue, it just means that it cannot be deduced
from the facts in the prolog database.

This backtracking behavior is more interesting
when you try to get a variable assignment.
Let's look at the following:

```
father(john, jack).
father(jack, james).
mother(mary, jack).


parent(X, Y) :- father(X, Y).
parent(X, Y) :- mother(X, Y).
ancestor(X, Y) :- parent(X, Y).
ancestor(X, Y) :- parent(X, Z), ancestor(Z, Y).

?ancestor(john, X).
```

The system returns all descendants of John:
```
VM: X = jack
VM: X = james
VM: false
```

And we can get all ancestors of James:
```
?ancestor(X, james).

VM: X = jack
VM: X = john
VM: X = mary
VM: false
```

Real prolog can do much more than this.
It's turing complete so a 'real' programming language.
The append function looks as follows:

```
app([], Z, Z).
app([H|XT], Y, [H|ZT]) :- app(XT, Y, ZT).

?app([1, 2, 3], [4, 5], L).

L = [1, 2, 3, 4, 5]
```

## How can you try it out?

```
String source = "YOUR CODE AND QUERY HERE";

Runner runner = new Runner();
// runner.activateDebugPrint();

runner.run(source);
```

## What does the compiled code look like?

Prolog code:
```
human(socrates).
human(aristotle).
mortal(X) :- human(X).
?mortal(X).
```

VM instructions:
```
Init _0
PushEnv 1
Mark _1
PutVar 1
Call mortal/1
_1: Halt 1
_0: No
mortal/1: PushEnv 1
Mark _2
PutRef 1
Call human/1
_2: PopEnv
human/1: SetBackTrackPoint
Try _3
DeleteBackTrackPoint
Jump _4
_3: PushEnv 1
PutRef 1
UAtom socrates
PopEnv
_4: PushEnv 1
PutRef 1
UAtom aristotle
PopEnv
```

**WARNING**

The order of facts and rules matters (real Prolog has
the same problem).

This terminates with 'true':
```
sibling(mary, john).
sibling(X, Y) :- sibling(Y, X).
?sibling(john, mary).
```

This crashes because Prolog runs out of stack space:
```
sibling(X, Y) :- sibling(Y, X).
sibling(mary, john).
?sibling(john, mary).
```
Why? Prolog tries to prove ```sibling(john, mary)``` by
proving ```sibling(mary, john)``` which it tries to
prove by ```sibling(john, mary)``` (it always tries the rules
in the way they are stated, so it's caught in an endless loop).

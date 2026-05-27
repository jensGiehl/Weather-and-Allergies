# Implementation Rules

## Language
All code, variable names, function names, class names, and any other identifiers must be written in English.

## Clean Code
- Use meaningful, intention-revealing names for variables, functions, and classes.
- Functions do one thing and do it well. Keep them small.
- Avoid duplication — extract repeated logic into reusable abstractions.
- Keep the level of abstraction consistent within a function.
- Prefer explicit over implicit. No magic numbers or strings — use named constants.
- Side effects must be obvious and minimal.
- Return early to reduce nesting depth.

## Object-Oriented Programming
- Model the domain with classes that have clear, single responsibilities (SRP).
- Favor composition over inheritance.
- Depend on abstractions, not concretions (DIP).
- Keep classes small and focused. A class should have one reason to change.
- Encapsulate state — expose behavior, not data.
- Use interfaces or abstract base classes to define contracts between components.

## No Comments
- Write no comments. Well-named identifiers are the documentation.
- If a comment seems necessary, rename the identifier or extract a function instead.
- Docstrings are also prohibited — the code must speak for itself.

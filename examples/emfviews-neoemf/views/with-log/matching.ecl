rule javaVariables
match t : log!Event
with  v : java!SingleVariableAccess {
  compare {
    return t.name = v.variable.name;
  }
}

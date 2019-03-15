//alias_java=http://www.eclipse.org/MoDisco/Java/0.2.incubation/java-neoemf
//alias_log=Log

rule javaVariables
match t : log!Event
with  v : java!SingleVariableAccess {
  compare {
    return t.name = v.variable.name;
  }
}
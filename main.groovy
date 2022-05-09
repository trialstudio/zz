def x = ["one": "two"]
def y = ["three": "four"]

println x
println y

x = x + y
y.three = "five"

println x
println y

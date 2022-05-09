def x = [['name':'two'], ['name': 'four']]
println x.collect {it.name}.join("','")

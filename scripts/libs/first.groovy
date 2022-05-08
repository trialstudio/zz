def first() {
  println 'first'
}

def x = new second()
x.second()

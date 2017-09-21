def enumerate2(xs, start=0, step=1):
    for x in xrange(start, len(xs), step):
        yield (start, xs[x])
        start += step
import csv
import matplotlib.pyplot as plt
import sys

graphs_to_make = [
    'bestMovesOnLevels.csv', 
    'timesDestroyed.csv', 
    'timesToFirstSolve.csv',
    'firstMovesOnLevels.csv',
    'timesUndoPressed.csv',
    'starsOnLevels.csv',
    'timesResetPressed.csv',
    'totalLevelTimes.csv'
    ]

def avg(lst):
    total = 0
    count = 0
    for e in lst:
        if e != '':
            count += 1
            total += int(e)
    if count == 0:
        return 0
    return total/float(count)

def avg_for_level(level):
    return avg([data[i][level] for i in range(num_players)])

def level_safe(level):
    ret = []
    for i in range(num_players):
        v = data[i][level]
        if v == '':
            v = 0
        ret.append(v)
    return ret

filename_prefixes = sys.argv[1:]

for graph in graphs_to_make:
    filenames = []
    for prefix in filename_prefixes:
        filenames.append(prefix + graph)

    data = []
    for filename in filenames:
        f = open(filename, 'rb')
        reader = csv.reader(f)
        for row in reader:
            if not row[0] == 'unique' and not row[0] == 'levelOrdinals':
                data.append(row[1:])

    num_levels = len(data[0])
    num_players = len(data)
    level_stats = [avg_for_level(i) for i in range(num_levels)]

    plt.plot(level_stats)
    plt.title('Average ' + graph)
    plt.show()

    all_lines = [level_safe(i) for i in range(num_levels)]
    plt.plot(all_lines)
    plt.title('All ' + graph)
    plt.show()

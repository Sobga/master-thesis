import json
import os

import matplotlib
import matplotlib.pyplot as plt

from matplots import plot_benchmark
from pgfplots import pgf_boxplot_indexing



BENCHMARK_DIR = "../benchmarks"

def matplotlib_setup():
    matplotlib.use("pgf")
    matplotlib.rcParams.update({
        "pgf.texsystem": "pdflatex",
        'font.family': 'serif',
        'text.usetex': True,
        'pgf.rcfonts': False,
    })

    SMALL_SIZE = 8
    MEDIUM_SIZE = 8
    BIGGER_SIZE = 10
    plt.rc('font', size=SMALL_SIZE)  # controls default text sizes
    plt.rc('axes', titlesize=MEDIUM_SIZE)  # fontsize of the axes title
    plt.rc('axes', labelsize=MEDIUM_SIZE)  # fontsize of the x and y labels
    plt.rc('xtick', labelsize=SMALL_SIZE)  # fontsize of the tick labels
    plt.rc('ytick', labelsize=SMALL_SIZE)  # fontsize of the tick labels
    plt.rc('legend', fontsize=SMALL_SIZE)  # legend fontsize
    plt.rc('figure', titlesize=BIGGER_SIZE)  # fontsize of the figure title
    plt.ticklabel_format(style='sci', axis='y', scilimits=(0, 0))

def write_file(filename, data):
    with open(filename, 'w') as file:
        file.write("x\ty\n")

        for idx, value in data:
            file.write(f'{idx}\t{value}\n')


# def simplify_mem_benchmark(benchmark):
#     fields = benchmark['FIELDS']
#     mem_type = 'Grow' if fields['N_SHRINK'] == 0 else 'Shrink'
#
#     sizes = fields['ACTUAL_SIZE']
#     b_name = benchmark['NAME'].split("-")[0]
#     for r_name, arr in benchmark['DATA'].items():
#         simplified = simplify_data(arr, sizes)
#
#         write_file(os.path.join("./SimplifiedData", f'{b_name}-{mem_type}-{r_name}.dat'), simplified)


def main():
    matplotlib_setup()
    filenames = sorted(os.listdir(BENCHMARK_DIR), reverse=True)

    with open(os.path.join(BENCHMARK_DIR, filenames[1]), "r") as file:
        benchmarks = json.load(file)
        file.close()

    for idx, benchmark in enumerate(benchmarks):
        name = benchmark['NAME'].split("-")[0]
        print(name)
        if 'BoxPlot' in name:
            print(benchmark['FIELDS'])
            pgf_boxplot_indexing(benchmark)
        else:
            fig = plot_benchmark(benchmark)
            if type(fig) == list:
                for j, f in enumerate(fig):
                    f.savefig(f'Figures/{name}_{idx}_{j}.pgf', format='pgf', bbox_inches='tight')
            else:
                fig.savefig(f'Figures/{name}_{idx}.pgf', format='pgf', bbox_inches='tight')
            # plt.show()
if __name__ == '__main__':
    main()

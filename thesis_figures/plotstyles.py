from typing import Tuple

import matplotlib as mpl

DATASTRUCTURES = ["ArrayList", "ConstantArray", "ConstantLazyArray", "Brodnik", "BrodnikPowerTwo", "Sitarski", "Tarjan",
                  "TestArray VB-Array"]
DISPLAY_NAMES = ["ARL", "CNA", "CBG", "BRD", "BP2", "HAT", "TJN", "VBA"]
N_MARKERS = 5
SYMBOLS = [".", "+", "d", "o", "x", "v", "^", "P"]
COLORS = ["red", "orange", "purple", "black", "brown", "blue", "green", "yellow"]
CMAP = mpl.colormaps['viridis']


def datastructure_idx(datastructure_name: str) -> int:
    datastructure_name = datastructure_name.split("-")[0]
    for i, name in enumerate(DATASTRUCTURES):
        if datastructure_name in name:
            return i
    raise Exception("No matching name for " + datastructure_name)


def display_name(datastructure_name: str):
    idx = datastructure_idx(datastructure_name)
    return f'\\textit{{{DISPLAY_NAMES[idx]}}}'


def lookup_style(datastructure_name: str):
    idx = datastructure_idx(datastructure_name)
    return COLORS[idx], SYMBOLS[idx]


def mark_freq(datastructure_name: str, n_points: int) -> Tuple[int, int]:
    idx = datastructure_idx(datastructure_name)
    spacing = n_points / N_MARKERS

    return int(spacing * idx / len(DATASTRUCTURES)), int(spacing)

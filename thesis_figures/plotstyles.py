from typing import Tuple

import matplotlib as mpl

DATASTRUCTURES = ["ArrayList", "ConstantArray", "ConstantLazyArray", "BrodnikPowerTwo", "Brodnik", "Sitarski", "Tarjan", "TestArray"]
N_MARKERS = 5
SYMBOLS = [".", "+", "d", "o", "x", "v", "^", "P"]
COLORS = ["red", "orange", "purple", "black", "brown", "blue", "green", "yellow"]
CMAP = mpl.colormaps['viridis']


def datastructure_idx(datastructure_name: str) -> int:
    for i, name in enumerate(DATASTRUCTURES):
        if name in datastructure_name:
            return i
    raise Exception("No matching name for " + datastructure_name)


def lookup_style(datastructure_name: str):
    idx = datastructure_idx(datastructure_name)
    return COLORS[idx], SYMBOLS[idx]


def mark_freq(datastructure_name: str, n_points: int) -> Tuple[int, int]:
    idx = datastructure_idx(datastructure_name)
    spacing = n_points / N_MARKERS

    return int(spacing * idx / len(DATASTRUCTURES)), int(spacing)

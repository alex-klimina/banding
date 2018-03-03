import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import matplotlib 
matplotlib.style.use('ggplot')

url = "dataForGraph"

with open(url) as f:
    for line in f:
        numbers_str = line.split(",")
        numbers_float = [float(x) for x in numbers_str]

plt.plot(numbers_float)
plt.savefig('graph.png', bbox_inches='tight') #сохраняем в формате png и удаляем пробельное изображение вокруг картинки из-за
plt.show() #важным использовать plt.show после сохранения фигуры, иначе это не сработает
plt.close()#способ предотвратить появление фигуры

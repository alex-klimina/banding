import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import matplotlib
matplotlib.style.use('ggplot')

#url = ссылка на файл или полный путь к файлу на компе
#читаем csv
graph =  pd.read_csv(url, names = ['val','date'], index_col=[1], decimal=',',
                 parse_dates = True, dayfirst = True)

graph.plot()
plt.savefig('graph.png', bbox_inches='tight')
plt.show()
plt.close()

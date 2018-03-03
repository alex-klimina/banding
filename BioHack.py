import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import matplotlib 
matplotlib.style.use('ggplot')

#url = ссылка на файл или полный путь к файлу на компе, не забыть раскомментить эту штуку
#читаем csv
graph =  pd.read_csv(url, names = ['val','date'], index_col=[1], decimal=',',
                 parse_dates = True, dayfirst = True) #отрисовываем график

graph.plot()
plt.savefig('graph.png', bbox_inches='tight') #сохраняем в формате png и удаляем пробельное изображение вокруг картинки из-за 
plt.show() #важным использовать plt.show после сохранения фигуры, иначе это не сработает
plt.close()#способ предотвратить появление фигуры 

import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt

data = pd.read_csv("results-diagonal.csv")
# Plot a scatterplot of 'Age' vs 'Income' columns
sns.scatterplot(data=data, x='distance', y='value')
plt.title('Probability in the Diagonal')
plt.show()
#plt.savefig('./f1.png')
#plt.show()
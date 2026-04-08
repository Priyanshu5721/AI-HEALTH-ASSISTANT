import pandas as pd
from sklearn.ensemble import RandomForestClassifier
import joblib

data = pd.read_csv("disease_dataset.csv")

X = data.drop("disease", axis=1)
y = data["disease"]

model = RandomForestClassifier()
model.fit(X, y)

joblib.dump(model, "disease_model.pkl")

print("Model trained and saved!")
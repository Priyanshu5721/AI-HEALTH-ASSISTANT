from flask import Flask, request, jsonify
import joblib

app = Flask(__name__)

model = joblib.load("disease_model.pkl")

@app.route('/predict', methods=['POST'])
def predict():
    data = request.json
    symptoms = data["symptoms"]

    prediction = model.predict([symptoms])

    return jsonify({
        "disease": prediction[0]
    })

if __name__ == '__main__':
    app.run(port=5000)
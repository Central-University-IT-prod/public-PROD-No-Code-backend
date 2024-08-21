FROM python:3.11-slim
WORKDIR /app

COPY app.py .
COPY requirements.txt .
COPY sessions/test_bot.session sessions/test_bot.session

RUN python3 -m pip install -r requirements.txt
RUN mkdir -p /app/sessions

CMD ["python3", "app.py"]
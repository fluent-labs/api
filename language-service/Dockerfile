FROM python:3.7

# Dependencies
RUN pip install "poetry==1.0.3"
WORKDIR /app
COPY poetry.lock pyproject.toml /app/
RUN poetry config virtualenvs.create false && poetry install --no-dev --no-interaction

COPY . /app/

EXPOSE 8000
CMD ["gunicorn", "LanguageService:app", "-b", ":8000"]

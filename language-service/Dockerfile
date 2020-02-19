FROM python:3.8-alpine3.11 as base

ENV PYTHONFAULTHANDLER=1 \
    PYTHONHASHSEED=random \
    PYTHONUNBUFFERED=1

WORKDIR /app

FROM base as builder

ENV PIP_DEFAULT_TIMEOUT=100 \
    PIP_DISABLE_PIP_VERSION_CHECK=1 \
    PIP_NO_CACHE_DIR=1 \
    POETRY_VERSION=1.0.3

RUN apk add --no-cache alpine-sdk libffi-dev openssl-dev python-dev py-pip
RUN pip install "poetry==$POETRY_VERSION"
RUN python -m venv /venv

# Hack to make sure cython is installed before any dependency that would need it.
RUN /venv/bin/pip install "cython==0.29.15"

COPY pyproject.toml poetry.lock ./
RUN poetry export --without-hashes -f requirements.txt | /venv/bin/pip install -r /dev/stdin

COPY . .
RUN poetry build && /venv/bin/pip install dist/*.whl

FROM base as final

# Make sure gunicorn is on the path
ENV PATH="/venv/bin:${PATH}"

# Runtime dependencies for compiled packages
RUN apk add --no-cache libstdc++

COPY --from=builder /venv /venv
COPY language_service /app/

EXPOSE 8000
CMD ["gunicorn", "LanguageService:app", "-b", ":8000"]

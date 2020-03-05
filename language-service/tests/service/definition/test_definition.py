import pytest
from unittest.mock import patch
from testfixtures import compare
from language_service.service.definition import get_definitions
from language_service.dto import Definition, ChineseDefinition


@patch("get_wiktionary_definitions", return_value="mockDefinition")
@patch("__builtin__.open", side_effect=open_file)
def test_can_pass_through_english_definitions():
    assert get_definitions("ENGLISH", "any") == "mockDefinition"

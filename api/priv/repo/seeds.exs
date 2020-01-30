# Script for populating the database. You can run it as:
#
#     mix run priv/repo/seeds.exs
#
# Inside the script, you can read and write to any of your
# repositories directly:
#
#     Api.Repo.insert!(%Api.SomeSchema{})
#
# We recommend using the bang functions (`insert!`, `update!`
# and so on) as they will fail if something goes wrong.

alias Api.{Repo, Language}

Repo.insert(Language.changeset(%Language{}, %{name: "Chinese"}))
Repo.insert(Language.changeset(%Language{}, %{name: "Danish"}))
Repo.insert(Language.changeset(%Language{}, %{name: "English"}))
Repo.insert(Language.changeset(%Language{}, %{name: "Spanish"}))

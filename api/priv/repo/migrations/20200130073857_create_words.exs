defmodule Api.Repo.Migrations.CreateWords do
  use Ecto.Migration

  def change do
    create table(:words) do
      add :text, :string
      add :part_of_speech, :string
      add :lemma, :string
      add :language, references(:languages, on_delete: :nothing)

      timestamps()
    end

    create index(:words, [:language])
    create index(:words, [:language, :text])
  end
end

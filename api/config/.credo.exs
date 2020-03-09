# This is a linter config.
# To run the linter, run: mix credo
%{
  configs: [
    %{
      name: "default",
      files: %{
        included: ["lib/"],
        excluded: ["test/support/"]
      },
      checks: [
        {Credo.Check.Consistency.TabsOrSpaces},
        {Credo.Check.Readability.MaxLineLength, priority: :low, max_length: 100},
        # Make TODO fail the build
        {Credo.Check.Design.TagTODO, exit_status: 2}
      ]
    }
  ]
}

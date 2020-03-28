defmodule Api.Logger do
  @moduledoc """
  Formatter that handles logging to JSON
  """
  @behaviour LoggerJSON.Formatter

  def format_event(level, message, timestamp, metadata, _metadata_keys) do
    %{
      time: format_timestamp(timestamp),
      levelname: level,
      message: IO.iodata_to_binary(message),
      file: Keyword.get(metadata, :file),
      function: Keyword.get(metadata, :function),
      line: Keyword.get(metadata, :line)
    }
  end

  # RFC3339 UTC "Zulu" format
  defp format_timestamp({date, time}) do
    [format_date(date), ?T, format_time(time), ?Z]
    |> IO.iodata_to_binary()
  end

  defp format_time({hh, mi, ss, ms}) do
    [pad2(hh), ?:, pad2(mi), ?:, pad2(ss), ?., pad3(ms)]
  end

  defp format_date({yy, mm, dd}) do
    [Integer.to_string(yy), ?-, pad2(mm), ?-, pad2(dd)]
  end

  defp pad3(int) when int < 10, do: [?0, ?0, Integer.to_string(int)]
  defp pad3(int) when int < 100, do: [?0, Integer.to_string(int)]
  defp pad3(int), do: Integer.to_string(int)

  defp pad2(int) when int < 10, do: [?0, Integer.to_string(int)]
  defp pad2(int), do: Integer.to_string(int)
end

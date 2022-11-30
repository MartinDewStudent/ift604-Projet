﻿using System.Text.Json.Serialization;

namespace BroomitModels;

public class Task
{
    /// <summary>
    /// Id of the task
    /// </summary>
    [JsonPropertyName("id")]
    public string? Id { get; set; } = Guid.NewGuid().ToString();

    /// <summary>
    /// Name of the task at hand
    /// </summary>
    [JsonPropertyName("name")]
    public string? Name { get; set; }

    /// <summary>
    /// Additionnal notes for usability purposes
    /// </summary>
    [JsonPropertyName("notes")]
    public string Notes { get; set; } = "";

    /// <summary>
    /// Schedule of the task
    /// <para />
    /// i.e. when it is scheduled for
    /// </summary>
    [JsonPropertyName("schedule")]
    public Schedule? Schedule { get; set; }

    /// <summary>
    /// Should the user be notified when task is due?
    /// </summary>
    [JsonPropertyName("notify_on_due")]
    public bool NotifyOnDue { get; set; } = false;

    public bool IsScheduled(DateOnly date) => Schedule == null ? false : Schedule.IsScheduled(date);
}

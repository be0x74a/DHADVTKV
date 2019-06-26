
msc {
  wordwraparcs=off,
  hscale=1.5;
  
  client [label="Client", linecolor="darkgreen", textcolor="white", textbgcolor="darkgreen", arclinecolor="darkgreen", arctextcolor="darkgreen"],
  partition [label="Partition",linecolor="maroon", textcolor="white", textbgcolor="maroon", arclinecolor="maroon", arctextcolor="maroon"];
  
  client => partition [label="GetRequest (H + 16)"];
  partition >> partition [label="CPU Delay (s) x 1"];
  partition => client [label="GetResponse (H + 1064)"];
  client >> client [label="CPU Delay (s) x #P"];
  
  client => partition [label="PrepareRequest (H + 1080)"];
  partition >> partition [label="CPU Delay (s) x 1"];
  partition => client [label="PrepareResponse (H + 9)"];
  client >> client [label="CPU Delay (s) x #P"];
  
  client => partition [label="CommitRequest (H + 33)"];
  partition >> partition [label="CPU Delay (s) x 1"];
  partition => client [label="CommitResponse (H)"];
  client >> client [label="CPU Delay (s) x #P"];
}


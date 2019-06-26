
msc {
  wordwraparcs=off,
  hscale=1.5;
  
  client [label="Client", linecolor="darkgreen", textcolor="white", textbgcolor="darkgreen", arclinecolor="darkgreen", arctextcolor="darkgreen"],
  partition [label="Partition",linecolor="maroon", textcolor="white", textbgcolor="maroon", arclinecolor="maroon", arctextcolor="maroon"],
  root [label="Root", linecolor="#3a5795", textcolor="white", textbgcolor="#3a5795", arclinecolor="#3a5795", arctextcolor="#3a5795"];
  
  client => partition [label="GetRequest (H + 16)"];
  partition >> partition [label="CPU Delay (s) x 1"];
  partition => client [label="GetResponse (H + 1064)"];
  client >> client [label="CPU Delay (s) x #P"];
  
  client => partition [label="Commit (H + 1092)"];
  partition >> partition [label="CPU Delay (s) x 1"];
  partition => root [label="BatchValidate ((H / B) + 57)"];
  root >> root [label="CPU Delay (s) x P / B"];
  
  root => partition [label="TransactionValidationBatch ((H / B) + 25)"];
  root => client [label="CommitResult (H + 17)"];
  partition >> partition [label="CPU Delay (s) x 1 / B"];
  client >> client [label="CPU Delay (s) x 1"];
 
}


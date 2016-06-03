<!DOCTYPE html>
<html>
<head>
<title>${reportName}</title>
<meta charset="utf-8">
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<link rel="stylesheet" href="css/foundation.css">
<link rel="stylesheet" href="css/daigoro.css">
</head>
<body>
<h2>${reportName}</h2>

<table border=1>
  <tr>
    <th></th>
    <#list timeStampList as timeStamp>
    <th>${timeStamp?index+1}</th>
    </#list>
  </tr>
  <#list threadList as thread>
  <tr>
    <td align="right">${thread}</td>
    <#list timeStampList as timeStamp>
    <td align="center" valign="center" class="${status[timeStamp?index][thread?index].status}">${status[timeStamp?index][thread?index].statusChar}</td>
    </#list>
  </tr>
</#list>
</table>

</body>
</html>
<!DOCTYPE html>
<html>
<head>
<title>${reportName} - ${timeStamp}</title>
<meta charset="utf-8">
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script type="text/javascript">
  $(document).ready(function() {
<#list threadList as threadName>
    $('#${threadFileNameMap[threadName]}').load('${timeStampDirName}/${threadFileNameMap[threadName]}');
</#list>
  });
</script>
<link rel="stylesheet" href="css/foundation.css">
<link rel="stylesheet" href="css/daigoro.css">
</head>
<body>
<h2>${reportName} - ${timeStamp}</h2>
<h4><a href="index.html">back to index</a></h4>
<#list threadList as threadName>
<pre id='${threadFileNameMap[threadName]}'></pre>
<p/>
</#list>

</body>
</html>
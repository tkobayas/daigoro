<!DOCTYPE html>
<html>
<head>
<title>${reportName} - ${threadName}</title>
<meta charset="utf-8">
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script type="text/javascript">
  $(document).ready(function() {
<#list timeStampList as timeStamp>
    $('#${timeStampDirNameMap[timeStamp]}').load('${timeStampDirNameMap[timeStamp]}/${threadFileName}');
</#list>
  });
</script>
<link rel="stylesheet" href="css/foundation.css">
<link rel="stylesheet" href="css/daigoro.css">
</head>
<body>
<h2>${reportName} - ${threadName}</h2>

<#list timeStampList as timeStamp>
<div id='${timeStampDirNameMap[timeStamp]}'></div>
<p/>
</#list>

</body>
</html>
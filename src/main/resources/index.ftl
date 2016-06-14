<!DOCTYPE html>
<html>
<head>
<title>${reportName}</title>
<meta charset="utf-8">
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script>
$(function(){
        $('#check1').prop('checked', false);
        $('#check2').prop('checked', false);
        $('#check3').prop('checked', false);
        $('.NATIVE').hide();
        $('.IDLE').hide();
        $('.UNCHANGED').hide();
});

function hideToggle(status) {
    $(status).toggle();
}
</script>
<link rel="stylesheet" href="css/foundation.css">
<link rel="stylesheet" href="css/daigoro.css">
</head>
<body>
<h2>${reportName}</h2>

<div class="checkbox">
    <label>
         Show hidden threads : &nbsp;
         <input id="check1" type="checkbox" checked="false" onclick="hideToggle('.NATIVE');">Native &nbsp;
         <input id="check2" type="checkbox" checked="false" onclick="hideToggle('.IDLE');">Idle &nbsp;
         <input id="check3" type="checkbox" checked="false" onclick="hideToggle('.UNCHANGED');">Unchanged &nbsp;
    </label>
</div>

<table border=1>
  <tr>
    <th></th>
    <#list timeStampList as timeStamp>
    <th>${timeStamp?index+1}</th>
    </#list>
  </tr>
  <#list threadList as thread>
  <tr class="${threadStatusMap[thread]}">
    <td align="right"><a href="${threadFileNameMap[thread]}.html">${thread}</a></td>
    <#list timeStampList as timeStamp>
    <td align="center" valign="center" class="${status[timeStamp?index][thread?index].status}">${status[timeStamp?index][thread?index].statusChar}</td>
    </#list>
  </tr>
</#list>
</table>

</body>
</html>
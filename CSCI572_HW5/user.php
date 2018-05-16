<?php
include 'SpellCorrector.php';
include_once('simple_html_dom.php');
require_once('solr-php-client/Apache/Solr/Service.php');

// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');
function myCmpFunc($a,$b){
    return strlen($b)-strlen($a);
}
$limit = 10;
$query = isset($_GET['q']) ? $_GET['q'] : false;
$autocomplete = isset($_GET['a']) ? $_GET['a'] : false;
$results = false;
$sug_result_list = false;
$sug_results = false;
if ($autocomplete) {
	$autocomplete = trim($autocomplete);
	$input = strtolower($autocomplete);
	$queryArray = explode(" ",$input);
	$tmp = "";
	foreach($queryArray as $item)
	{
	   $tmp = $tmp . " " . SpellCorrector::correct($item);
	}
	if(strcmp(strtolower($autocomplete),strtolower(trim($tmp)))) {
		$correctSpell = $tmp;    
	} else {
		$correctSpell = "";	
	}
	$prevWord = "";
	if(count($queryArray) == 1) {
		$queryWord = $queryArray[0];
	} else {
		$index = 0;
		while($index < (count($queryArray) - 1)) {
			$prevWord = $prevWord . " " . $queryArray[$index];
			$index = $index + 1;		
		}
		$preWord = trim($prevWord);
		$preWord = $preWord." ";
		$queryWord = $queryArray[$index];
	}
	$solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample/');
	 // if magic quotes is enabled then stripslashes will be needed
	 if (get_magic_quotes_gpc() == 1)
	 {
	 	$queryWord = stripslashes($queryWord);
	 }
	 try
	 {
		 //$results = $solr->search($query, 0, $limit);
		 $additionalParameters = array(
		  'qt' => 'suggest',

		 );
		 $sug_results = $solr->search($queryWord, 0, $limit, $additionalParameters);
		 if(!empty($correctSpell)) {
		 	echo "<p style='margin:0px;'><i><b><a style='color:black;text-decoration:none;' href='user.php?q=".$correctSpell."'>".$correctSpell."</a></b></i></p>";
		 }
		 foreach($sug_results->suggest->suggest->$queryWord->suggestions as $item) {
		 	echo "<p style='margin:0px;'><a style='color:black;text-decoration:none;' href='user.php?q=".$preWord.$item->term."'>".$preWord.$item->term."</a></p>";
		 }
		 
	}catch (Exception $e)
	{
		die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
	}
	return;
}
if ($query)
{
	 $query = trim($query);
	 $userInput = $query;
	 $queryArray = explode(" ",$query);
	 $tmp = "";
	 foreach($queryArray as $item)
	 {
	   $tmp = $tmp . " " . SpellCorrector::correct($item);
	 }
	 $userInput = $query;
	 $resQuery = trim($tmp);
	 $showSpellCheckText = false;
	 if(strcmp(strtolower($userInput),strtolower($resQuery))) {
	    $showSpellCheckText = true;
	 }
	 
	 $solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample/');
	 // if magic quotes is enabled then stripslashes will be needed
	 $item = "";
	 if (get_magic_quotes_gpc() == 1)
	 {
	 	$item = stripslashes($resQuery);
	 } else {$item = $resQuery;}
	 try
	 {
		$results = $solr->search($item, 0, $limit);
	 }
	 catch (Exception $e)
	{
	 	die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
	 }
}
?>
<html>
 <head>
 	<title>PHP Solr Client Example</title>
	<script>
		function showResult(str) {
		   if (str.length==0) { 
		     document.getElementById("livesearch").innerHTML="";
		     document.getElementById("livesearch").style.border="0px";
		     return;
		   }
		   if (window.XMLHttpRequest) {
		     // code for IE7+, Firefox, Chrome, Opera, Safari
		     xmlhttp=new XMLHttpRequest();
		   } else {  // code for IE6, IE5
		     xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
		   }
		   xmlhttp.onreadystatechange=function() {
		     if (this.readyState==4 && this.status==200) {
		       document.getElementById("livesearch").innerHTML=this.responseText;
		       document.getElementById("livesearch").style.border="1px solid #A5ACB2";
		     }
		   }
		   xmlhttp.open("GET","user.php?a="+str,true);
		   xmlhttp.send();
		 }
	 </script>
 </head>
 <body>
	 <form accept-charset="utf-8" method="get" style="display: inline-block;">
		<div style="display: block;float: left;"><label for="q">Search:</label></div>
		<div style="display: block;float: left;">		 
			<input id="q" name="q" size="30" onkeyup="showResult(this.value)" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
			<div id="livesearch">
			</div>
		</div>
		<div style="display: block;float: left;">
		 	<input type="submit"/>
		</div>
		 
	 </form>
	<br/>	
	<br/>
	<br/>

		<?php
		// display results
		if ($results)
		{
			 $total = (int) $results->response->numFound;
			 $start = min(1, $total);
			 $end = min($limit, $total);
		?>
			 <div style="display: inline-block;"><?php if($showSpellCheckText) { ?>Showing result of <b><i><?php echo $resQuery ?> </i></b> instead of <b><i><?php echo $userInput; ?> </i></b><br/><br/><?php } ?>
			 Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:
			 <ol>

				<?php
				 $tmp_array = array(); 

				 // iterate result documents
				 foreach ($results->response->docs as $doc)
				 {
				  
				  $lines = array();
				  $html = file_get_html($doc->og_url);
				  $ret = $html->find('div[class="article-body"]');
				  foreach($ret as $div) {
				  	foreach($div->children() as $node) {
						if($node->tag=='p'){						
							$lineArray = explode(".",strip_tags($node));
							$lines =  array_merge($lines, $lineArray);
						}					
					}

				  }
				  usort($lines,'myCmpFunc');
				  $maxPos = -1;
				  $maxVal = -1;	
				  $tmpQuery = explode(" ",$resQuery);
				  $description = "";
				  $index = 0;
				  foreach($lines as $line) {
					$line = strtolower(trim($line));
					$index = $index + 1;
				  	$val = 0;
					foreach($tmpQuery as $item) {
						if(preg_match('/\b'.strtolower($item).'\b/', strtolower($line), $matches)){
							$val = $val + 1;						
						}					
					}
					if($val == count($tmpQuery)) {
						$description = $line;	
						break;				
					}
					if(($val > $maxVal) && ($val != 0)) {
						$maxVal = $val;
						$maxPos = $index;
					}
				  }
				  if(empty($description)) {
					  if($maxPos != -1) { $maxPos;$description = $lines[$maxPos];} 
				  }
				  //echo "Description:".$description;
				  if(!in_array($doc->id, $tmp_array)){array_push($tmp_array, $doc->id);
					$id = $doc->id;
					if(empty($description)) {
						$description = strtolower($doc->dc_description);
					} else {
						$description = str_replace("<p>", "",strtolower($description));
						$description = str_replace("</p>", "",strtolower($description));
					}
					$isValid = false;
					foreach($tmpQuery as $item) {
						if(preg_match('/\b'.strtolower($item).'\b/', strtolower($description), $matches)){
							$isValid = true;
							break;						
						}					
					}
					if(!$isValid) {
						$description = $doc->title;
						foreach($tmpQuery as $item) {
							if(preg_match('/\b'.strtolower($item).'\b/', strtolower($description), $matches)){
								$isValid = true;
								break;
							}					
						}
						if(!$isValid) {
							$description = "N/A";						
						}					
					}
					if(strlen($description) > 150) {
						foreach($tmpQuery as $item) {
							$pos = strpos(strtolower($description), strtolower($item));
							if($pos !== false) {
								$tmp = substr($description, $pos, 150);
								$tmp1 = $tmp;
								if($pos !== 0) {$tmp = "...".$tmp;}
								else {$tmp = $tmp."...";}
								$description = $tmp;
								break;
								 
							}						
						}
						if(strlen($description) > 160){
							$description = substr($description, 0, 150)."...";
						}	
					}
					if(empty($description)) {$description="N/A";} 
					$description = strtolower($description);
					foreach($tmpQuery as $item) {
						$description = preg_replace('/\b'.strtolower($item).'\b/', '<strong>'.strtolower($item).'</strong>', $description);
											
					}
					
					
				?>
				 <li>
				 <p>
				 <a href="<?php echo htmlspecialchars($doc->og_url, ENT_NOQUOTES, 'utf-8'); ?>"><?php echo htmlspecialchars($doc->title, ENT_NOQUOTES, 'utf-8'); ?></a>
				 <br/>
				 <a style="color:#006621;text-decoration:none;" href="<?php echo htmlspecialchars($doc->og_url, ENT_NOQUOTES, 'utf-8'); ?>"><?php echo htmlspecialchars($doc->og_url, ENT_NOQUOTES, 'utf-8'); ?></a>
				 <br/>
				 <?php echo $description ?>
				 <br/>
				 <span style="color:#AAA;"><i><?php echo htmlspecialchars($doc->id, ENT_NOQUOTES, 'utf-8'); ?></i></span>
				 </p>
				 </li>
				<?php
				 }}
				?>
			 </ol></div>
		<?php
		}
		?>
	 </body>
</html>

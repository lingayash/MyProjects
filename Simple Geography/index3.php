<!DOCTYPE html>
<html lang = "en">
<head>
	<title>SIMPLE GEOGRAPHY</title>
	<meta charset="utf-8">
	<meta name="description" content="GEOGRAPHY">
	<meta name="keywords" content="GEOGRAPHY">
	<link rel="stylesheet" href="final.css">
	<meta name="author" content="Shiva Lingala" />
</head>
<body id="back">
	<p style="float:right;color:white">
		<a href="documentation.html">Documentation</a>
	</p>
	<div class="container">
		<div id="head">
		<h1>Simple Geography</h1>
		<h2>Click on any of the following flags to learn more about the country's </br>
		<a class="menu" href="index.php">Geographic Location</a>,
		<a class="menu" href="index2.php">Map</a>
		or
		<a class="menu" href="index3.php">Emblem</a>
		(<--Click To Select)
		</h2>
		</div>
	
		<?php
			$db_conn = mysql_connect("localhost", "root", "");
			if (!$db_conn)
				die("Unable to connect: " . mysql_error()); 
			mysql_query("CREATE DATABASE smartdb", $db_conn);
			mysql_select_db("smartdb", $db_conn);
			$cmd = "CREATE TABLE images (
				  country varchar(30) NOT NULL PRIMARY KEY,
				  flag varchar(30),
				  map varchar(30),
				  location varchar(30),
				  emblem varchar(30)
				)";
			mysql_query($cmd);
			$cmd = "LOAD DATA LOCAL INFILE 'img_locations.csv' INTO TABLE images
					FIELDS TERMINATED BY ','";
			mysql_query($cmd);
			
			$cmd = "SELECT * FROM images";
			$flags = mysql_query($cmd);
			$counter = 0;
			echo "<table>". PHP_EOL;
			echo "<ul>". PHP_EOL;
			while($row = mysql_fetch_array($flags)){
				if($counter%4 == 0){
					echo"<tr>". PHP_EOL;
					echo"<td><a href=\"Data/" . $row['emblem'] . "\" class=\"lightbox_trigger\"><img class=\"flag\" src=\"Data/" . $row['flag'] . "\"><p class=\"name\">". strtoupper ($row['country']) ."</p></a></td>". PHP_EOL;
				}
				elseif($counter%4 == 3){
					echo"<td><a href=\"Data/" . $row['emblem'] . "\" class=\"lightbox_trigger\"><img class=\"flag\" src=\"Data/" . $row['flag'] . "\"><p class=\"name\">". strtoupper ($row['country']) ."</p></a></td>". PHP_EOL;
					echo"</tr>". PHP_EOL;
				}
				else{
					echo"<td><a href=\"Data/" . $row['emblem'] . "\" class=\"lightbox_trigger\"><img class=\"flag\" src=\"Data/" . $row['flag'] . "\"><p class=\"name\">". strtoupper ($row['country']) ."</p></a></td>". PHP_EOL;
				}
				$counter++;
				
			}
			echo "</ul>". PHP_EOL;
			echo"</table>". PHP_EOL;
			mysql_close($db_conn);
		?>
	
	</div>
	<!--	I got the following code from http://webdesign.tutsplus.com/tutorials/htmlcss-tutorials/super-simple-lightbox-with-css-and-jquery/  -->
	<script src="http://code.jquery.com/jquery-1.6.2.min.js"></script>
	<script>
	jQuery(document).ready(function($) {
		
		$('.lightbox_trigger').click(function(e) {
			
			//prevent default action (hyperlink)
			e.preventDefault();
			
			//Get clicked link href
			var image_href = $(this).attr("href");
			
			/* 	
			If the lightbox window HTML already exists in document, 
			change the img src to to match the href of whatever link was clicked
			
			If the lightbox window HTML doesn't exists, create it and insert it.
			(This will only happen the first time around)
			*/
			
			if ($('#lightbox').length > 0) { // #lightbox exists
				
				//place href as img src value
				$('#content').html('<img src="' + image_href + '" />');
				
				//show lightbox window - you could use .show('fast') for a transition
				$('#lightbox').show();
			}
			
			else { //#lightbox does not exist - create and insert (runs 1st time only)
				
				//create HTML markup for lightbox window
				var lightbox = 
				'<div id="lightbox">' +
					'<p>Click to close</p>' +
					'<div id="content">' + //insert clicked link's href into img src
						'<img src="' + image_href +'" />' +
					'</div>' +	
				'</div>';
					
				//insert lightbox HTML into page
				$('body').append(lightbox);
			}
			
		});
		
		//Click anywhere on the page to get rid of lightbox window
		$('#lightbox').live('click', function() { //must use live, as the lightbox element is inserted into the DOM
			$('#lightbox').hide();
		});

	});
	</script>

</body>

</html>
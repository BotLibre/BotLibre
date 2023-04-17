<div id="box" style="padding: 10px; border: 5px solid rgb(163, 43, 43); width: 93%;">
    <strong>Comment: </strong> <?php echo $debugComment; ?> <br>
    <?php 
        if(isset($debugInfo)) {
            echo print_r($debugInfo); 
        }
    ?>
</div>
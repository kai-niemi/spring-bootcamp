set cluster setting kv.rangefeed.enabled = true;

create changefeed for table purchase_order
    into '${cdc-sink-url}?topic_name=purchase_order'
    with envelope=enriched, key_in_value;
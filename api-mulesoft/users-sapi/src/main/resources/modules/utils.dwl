fun queryStringByParams(params: Object, values: Object) = do {
    var query = ((params pluck values[$$]) -- [null]) joinBy " AND " 
    ---
    if(isBlank(query)) "" else " WHERE $(query)" 
}

fun queryStringByParams(params: Null, values: Object) = ""
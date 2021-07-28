# Error Codes and Tips to Resolve

## `100-199` - Internal Data Loader Errors (client side setup)

### `101` - Failure to login

Check username and password by logging into bullhornstaffing.com. Verify that the clientId, clientSecret are correct.

### `110` - Invalid CSV file

Verify that the CSV file has the correct number of columns and is saved in one of the supported formats: UTF-8 (recommended multi-byte format) or ISO-8859-1 (legacy single-byte support).

### `120` - Missing configuration value

## `200-299` - Missing / Invalid data in Bullhorn

### `201` - Cannot locate entity for updating

### `202` - Too many matching records found for this row

The duplicate check found more than one existing record to update. Each row in the CSV file should correspond to a single record in Bullhorn. Narrow the search to only the single record that should be updated for the given row.

### `203` - Cannot find associated entity

## `301-399` - Real-time processing error

### `301` - Internet connectivity issues

## `400-499` - Bad request

### `400` - Generic bad request

### `401` - Missing required property

### `410` - Duplicate effective date

## `500-599` - Internal server error

### `501` - Generic server error

### `502` - 

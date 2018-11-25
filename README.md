# S3 File Upload Serverless

A serverless function that generates Pre-Signed Upload URLs for a configured S3 bucket.

## Usage

```bash
curl $HOST/upload -X POST \
    -d "{\"fileName\": \"hello-world.txt\"}" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json"
```

**Response:**

```json
{
    "fileName": "hello-world.txt",
    "key": "79be4015-d3d8-43e2-8e90-ed9c84f41658.txt",
    "expires": 1546300799,
    "url": "https://..."
}
```

The `expires` attribute contains a UNIX timestamp, indicating when the URL will expire to work.

##  Configuration

Create a file called `src/main/resourcey/config.edn` (see [the example file](src/main/resources/config.example.edn)).

```edn
{:bucket "my-temporary-bucket"
 :region "us-east-1"
 :url-expirery-time 7200}
```

| Attribute | Type | Comment |
| --------- | ---- | ------- |
| `bucket` | AWS S3 Bucket Name | The bucket to generate the Pre-Signd Upload URLs for. |
| `region` | AWS Region ID | ID of the region the bucket is placed in. |
| `url-expirery-time` | Natural Number / Seconds | The number of seconds a Pre-Signed URL will be valid. By default 1 hour. |
import requests
import json

BASE_URL = "http://localhost:8080"

# DATA = bytearray(41943040)  # 40 mb
# DATA = bytearray(32)  # 40 mb

# http://bbb3d.renderfarming.net/download.html
with open("bbb_sunflower_1080p_30fps_normal.mp4", "rb") as fd:
    # DATA = fd.read()[:6291456*3 + 64]  # TODO Only read what is necessary
    DATA = fd.read()

upload = requests.post(
    BASE_URL + "/v1/uploads",
    json={
        "name": "bbb_sunflower_1080p_30fps_normal.mp4",
        "size": len(DATA),
        "preferredChunkSize": 6291456,
    }
).json()

print(json.dumps(upload, indent=4))

upload_id = upload["id"]
hashes = []

for chunk in upload["chunks"]:
    print(chunk)
    data_start = chunk["offset"]
    data_end = data_start + chunk["size"]

    presigned_request = requests.get(BASE_URL + f"/v1/uploads/{upload_id}/chunks/{chunk['number']}/request").json()
    # print(data_start, data_end, len(DATA[data_start:data_end]))

    data_response = requests.request(
        presigned_request["method"],
        presigned_request["url"],
        headers=presigned_request["headers"],
        data=DATA[data_start:data_end]
    )
    # print(data_response)

    hash = data_response.headers["etag"]
    print(data_response.status_code, data_response.text, hash)

    # presigned_request = requests.get(BASE_URL + f"/v1/uploads/{upload_id}/chunks/{chunk['number']}/request").json()
    # print(data_start, data_end)
    # print(len(DATA[data_start:data_end]))

    chunk = requests.post(
        BASE_URL + f"/v1/uploads/{upload_id}/chunks/{chunk['number']}/confirm",
        json={
            "hash": hash
        }
    ).json()

    # hashes.append(hash.replace("\"", ""))

print(hashes)
upload = requests.post(
    BASE_URL + f"/v1/uploads/{upload_id}/complete",
).json()

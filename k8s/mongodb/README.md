# MongoDB Kubernetes

---

## How to generate secret

```shell=
echo "devopscube" | base64 
//after encoding it, this becomes ZGV2b3BzY3ViZQo=
echo "ZGV2b3BzY3ViZQo=" | base64 --decode
//after decoding it, this will give devopscube
```
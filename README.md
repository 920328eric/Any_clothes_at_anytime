# 這是一款名為「時尚試衣鏡」的智慧型手機應用程式
單純利用手機鏡頭照身體，將網路上欲試穿的衣物直接套在使用者身上，並有語音提示讓使用者找到最佳穿衣位置

![專案封面圖](https://github.com/920328eric/Any_clothes_at_anytime/assets/114470260/122748b6-b332-41a9-9cfc-827423ac5638)

## 使用前說明
1. 目前還未寫登入介面進行身份驗證，所以無法靠使用者介面上傳衣服和褲子的圖片以達成連動
2. 衣服和褲子圖片可以自行上網下載，放到程式碼中的ChangeClothes 的 載入所有衣服、褲子的資源（val clothingResources = 、val pantsResources = 的 arrayOf 裡）
3. 記得加上自己的google-services.json，以連接自己的 Firebase 資料庫

## 畫面
1. 初始介面

![範例圖片 1](https://github.com/920328eric/Any_clothes_at_anytime/assets/114470260/470173a3-53eb-4357-bf85-6b23ba64ad33)

2. 試衣間

![範例圖片 2](https://github.com/920328eric/Any_clothes_at_anytime/assets/114470260/dbc0fa6a-8deb-4145-bcd2-f2f060062b53)
![範例圖片 3](https://github.com/920328eric/Any_clothes_at_anytime/assets/114470260/f35ce6dd-f190-4885-9e1c-be040502dfc8)

3. 衣櫃（目前還無法連動），但可以點選右上方的重整按鈕，隨機搭配衣物

![範例圖片 4](https://github.com/920328eric/Any_clothes_at_anytime/assets/114470260/f87d5bec-8b70-4c26-97d5-32f220f3f3c1)


## 操作說明

使用者透過試衣間功能進行衣物穿搭之模擬，系統會透過語音提示使用者須往前或往後，使衣物圖片的大小更貼近其身體比例。若要更換上一件或下一件衣服，只需將手停滯在畫面中的按鈕處達3 秒，系統即會加載以更替衣物，而更替褲子也是相同的做法。（不需透過按手機畫面，待在原地就可以）

## 支援

目前僅支援Android手機

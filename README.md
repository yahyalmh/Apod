# Android Apod
An Android app to download the Nasa daily image and video 

## Table of content

* [Introduction](#Introduction)

* [Technologies](#Technologies)
 
* [Screenshots](#Screenshots)

* [To Do](#To-Do)

## Introduction
 Nasa releases images and videos daily, and there is an API for that. This app uses that API to fetch photos and videos, and you can see an infinite list of them, watch videos, download images, set images as wallpaper, or choose your favorites. 

## Technologies 
* UI: Sigle activity, Fragments, Custom components, Animation, Lottie files
* Architecture: MVVM, Hilt, LiveData, Flow, Room, ViewModel, Paging, Navigation, Lifecycle, DataBinding, ViewBinding, Provider
* Network: Retrofit
* Foundation: Kotlin, AndroidX, AppCompat
* Behavior: Permission, DownloadManager, Sharing, Set Wallpaper, Swipe to refresh, Retry, ...

## Screenshot
<table  style="border: 1px solid black; width: 100%; word-wrap:break-word;
              table-layout: fixed; text-align:center" >
 <tr>
    <td style="text-align:center">Home fragment</td>
     <td style="text-align:center">Favorite fragment</td>
     <td style="text-align:center">Image detail fragment</td>
      <td style="text-align:center">Video detail fragment</td>
  </tr>
  <tr>
    <td style="text-align:center"><img src="screenshots/home_fragment_view.png" width=200 height=400></td>
    <td style="text-align:center"><img src="screenshots/favorite_fragment_view.png" width=200 height=400></td>
    <td style="text-align:center"><img src="screenshots/image_detail_view.png" width=200 height=400></td>
    <td style="text-align:center"><img src="screenshots/video_detail_view.png" width=200 height=400></td>
  </tr>
  <tr>
    <td style="text-align:center">Set wallpaper dialog</td>
     <td style="text-align:center">Open downloaded file dialog </td>
     <td style="text-align:center">Share Image or video's link view</td>
      <td style="text-align:center">YouTube unavailable and retry page</td>
  </tr>
  <tr>
    <td style="text-align:center"><img src="screenshots/set_wallpaper_dialog.png" width=200 height=400></td>
    <td style="text-align:center"><img src="screenshots/open_file_dialog.png" width=200 height=400></td>
    <td style="text-align:center"><img src="screenshots/share_view.png" width=200 height=400></td>
    <td style="text-align:center"><img src="screenshots/youtube_error_page.png" width=200 height=400></td>
  </tr>
 </table>
<!-- ![Home Fragment](screenshots/home_fragment_view.png "A list of Apods")
![Favorite Fragment](screenshots/favorite_fragment_view.png "List of favorite Apods")
![Image Detail](screenshots/image_detail_view.png "An image detail page")
![Image Detail](screenshots/video_detail_view.png "An video detail page") -->

## To Do
 * Add setting page
      * language 
      * schedule changing wallpaper(via WorkManger)
 * Open videos in youtube

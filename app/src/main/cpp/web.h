
#ifndef FUNNY_WEB_H
#define FUNNY_WEB_H

#include <arpa/inet.h>          /* inet_ntoa */
#include <signal.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <time.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/sendfile.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#define  LISTENQ 1024
#define MAXLINE 1024
#define  RIO_BUFSIZE 1024

typedef struct {
    int rio_fd;
    int rio_cnt;
    char *rio_bufptr;
    char rio_buf[RIO_BUFSIZE];
} rio_t;
typedef struct sockaddr SA;
typedef struct {
    char filename[512];
    off_t offset;
    size_t end;
} http_request;
typedef struct {
    const char *extension;
    const char *mime_type;
} mime_map;
mime_map meme_types[] = {
        {".css",  "text/css"},
        {".gif",  "image/gif"},
        {".htm",  "text/html"},
        {".html", "text/html"},
        {".jpeg", "image/jpeg"},
        {".jpg",  "image/jpeg"},
        {".ico",  "image/x-icon"},
        {".js",   "application/javascript"},
        {".pdf",  "application/pdf"},
        {".mp4",  "video/mp4"},
        {".png",  "image/png"},
        {".svg",  "image/svg+xml"},
        {".xml",  "text/xml"},
        {NULL, NULL},
};
char *default_mime_type = "text/plain";

void rio_readinitb(rio_t *rp, int fd) {
    rp->rio_fd = fd;
    rp->rio_cnt = 0;
    rp->rio_bufptr = rp->rio_buf;
}

ssize_t writen(int fd, void *usrbuf, size_t n) {
    size_t nleft = n;
    ssize_t nwritten;
    char *bufp = (char *) usrbuf;
    while (nleft > 0) {
        if ((nwritten = write(fd, bufp, nleft)) <= 0) {
            if (errno == EINTR)
                nwritten = 0;
            else
                return -1;
        }
        nleft -= nwritten;
        bufp += nwritten;
    }
    return n;
}

static ssize_t rio_read(rio_t *rp, char *usrbuf, size_t n) {

}

#endif //FUNNY_WEB_H

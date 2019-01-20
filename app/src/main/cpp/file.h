#ifndef FUNNY_FILE_H
#define FUNNY_FILE_H

static const char INVALID_FILE_CHARS[] = {
        '"', '<', '>', '|', '\0', '\x0001', '\x0002',
        '\x0003', '\x0004', '\x0005', '\x0006', '\a', '\b', '\t',
        '\n', '\v', '\f', '\r', '\x000e', '\x000f', '\x0010',
        '\x0011', '\x0012', '\x0013', '\x0014', '\x0015', '\x0016', '\x0017',
        '\x0018', '\x0019', '\x001a', '\x001b', '\x001c', '\x001d', '\x001e',
        '\x001f', ':', '*', '?', '\\', '/'};

void substringBeforeLast(char *s, char c, size_t n) {
    char *cp;
    if (n != 0) {
        cp = s + n;
        do {
            if (*(--cp) == c) {
                *cp = '\0';
                break;
            }
        } while (--n != 0);
    }
}

void getValidFileName(char *path, char replace) {
    char *src, *dst;
    for (src = dst = path; *src != '\0'; src++) {
        char c = *src;

        *dst = c;
        int found = 0;
        for (int i = 0; i < 41; i++) {
            if (INVALID_FILE_CHARS[i] == c) {
                found = 1;
                break;
            }
        }
        if (found)
            *dst = replace;
        dst++;
    }
    *dst = '\0';
}

#endif //FUNNY_FILE_H

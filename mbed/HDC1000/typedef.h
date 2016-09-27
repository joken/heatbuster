#ifndef TYPEDEF_H


#define TYPEDEF_H

typedef unsigned char       BYTE;
typedef unsigned short int   WORD;

typedef union {
  WORD word_value;
  BYTE byte_value[2];
  struct {
    BYTE LB;
    BYTE HB;
  } byte;
} WORD_VALUE;


#endif

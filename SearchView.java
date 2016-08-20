/**
 * Author: Liuxigang
 * Created Date: 16/6/8
 * GitHub: https://github.com/beaters
 */
public class SearchView extends View {

    // ����
    private Paint mPaint;

    // View ���
    private int mViewWidth;
    private int mViewHeight;

    // �����ͼӵ�е�״̬
    public static enum State {
        NONE,
        STARTING,
        SEARCHING,
        ENDING
    }

    // ��ǰ��״̬(�ǳ���Ҫ)
    private State mCurrentState = State.NONE;

    // �Ŵ����ⲿԲ��
    private Path path_srarch;
    private Path path_circle;

    // ����Path ����ȡ���ֵĹ���
    private PathMeasure mMeasure;

    // Ĭ�ϵĶ�Ч���� 2s
    private int defaultDuration = 2000;

    // ���Ƹ������̵Ķ���
    private ValueAnimator mStartingAnimator;
    private ValueAnimator mSearchingAnimator;
    private ValueAnimator mEndingAnimator;

    // ������ֵ(���ڿ��ƶ���״̬,��Ϊͬһʱ����ֻ������һ��״̬����,������ֵ����ȡ���ڵ�ǰ״̬)
    private float mAnimatorValue = 0;

    // ��Ч���̼�����
    private ValueAnimator.AnimatorUpdateListener mUpdateListener;
    private Animator.AnimatorListener mAnimatorListener;

    // ���ڿ��ƶ���״̬ת��
    private Handler mAnimatorHandler;

    // �ж��Ƿ��Ѿ���������
    private boolean isOver = false;

    private int count = 0;

    public SearchView(Context context) {
        super(context);

        initPaint();

        initPath();

        initListener();

        initHandler();

        initAnimator();

        // ���뿪ʼ����
        mCurrentState = State.STARTING;
        mStartingAnimator.start();

    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(15);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
    }

    private void initPath() {
        path_srarch = new Path();
        path_circle = new Path();

        mMeasure = new PathMeasure();

        // ע��,��Ҫ��360��,�����ڲ����Զ��Ż�,��������ȡ����Ҫ����ֵ
        RectF oval1 = new RectF(-50, -50, 50, 50);          // �Ŵ�Բ��
        path_srarch.addArc(oval1, 45, 359.9f);

        RectF oval2 = new RectF(-100, -100, 100, 100);      // �ⲿԲ��
        path_circle.addArc(oval2, 45, -359.9f);

        float[] pos = new float[2];

        mMeasure.setPath(path_circle, false);               // �Ŵ󾵰��ֵ�λ��
        mMeasure.getPosTan(0, pos, null);

        path_srarch.lineTo(pos[0], pos[1]);                 // �Ŵ󾵰���

        Log.i("TAG", "pos=" + pos[0] + ":" + pos[1]);
    }

    private void initListener() {
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        };

        mAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // getHandle����Ϣ֪ͨ����״̬����
                mAnimatorHandler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
    }

    private void initHandler() {
        mAnimatorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (mCurrentState) {
                    case STARTING:
                        // �ӿ�ʼ����ת������������
                        isOver = false;
                        mCurrentState = State.SEARCHING;
                        mStartingAnimator.removeAllListeners();
                        mSearchingAnimator.start();
                        break;
                    case SEARCHING:
                        if (!isOver) {  // �������δ���� �����ִ����������
                            mSearchingAnimator.start();
                            Log.e("Update", "RESTART");

                            count++;
                            if (count>2){       // count����2��������״̬
                                isOver = true;
                            }
                        } else {        // ��������Ѿ����� ������������
                            mCurrentState = State.ENDING;
                            mEndingAnimator.start();
                        }
                        break;
                    case ENDING:
                        // �ӽ�������ת��Ϊ��״̬
                        mCurrentState = State.NONE;
                        break;
                }
            }
        };
    }

    private void initAnimator() {
        mStartingAnimator = ValueAnimator.ofFloat(0, 1).setDuration(defaultDuration);
        mSearchingAnimator = ValueAnimator.ofFloat(0, 1).setDuration(defaultDuration);
        mEndingAnimator = ValueAnimator.ofFloat(1, 0).setDuration(defaultDuration);

        mStartingAnimator.addUpdateListener(mUpdateListener);
        mSearchingAnimator.addUpdateListener(mUpdateListener);
        mEndingAnimator.addUpdateListener(mUpdateListener);

        mStartingAnimator.addListener(mAnimatorListener);
        mSearchingAnimator.addListener(mAnimatorListener);
        mEndingAnimator.addListener(mAnimatorListener);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawSearch(canvas);
    }

    private void drawSearch(Canvas canvas) {

        mPaint.setColor(Color.WHITE);


        canvas.translate(mViewWidth / 2, mViewHeight / 2);

        canvas.drawColor(Color.parseColor("#0082D7"));

        switch (mCurrentState) {
            case NONE:
                canvas.drawPath(path_srarch, mPaint);
                break;
            case STARTING:
                mMeasure.setPath(path_srarch, false);
                Path dst = new Path();
                mMeasure.getSegment(mMeasure.getLength() * mAnimatorValue, mMeasure.getLength(), dst, true);
                canvas.drawPath(dst, mPaint);
                break;
            case SEARCHING:
                mMeasure.setPath(path_circle, false);
                Path dst2 = new Path();
                float stop = mMeasure.getLength() * mAnimatorValue;
                float start = (float) (stop - ((0.5 - Math.abs(mAnimatorValue - 0.5)) * 200f));
                mMeasure.getSegment(start, stop, dst2, true);
                canvas.drawPath(dst2, mPaint);
                break;
            case ENDING:
                mMeasure.setPath(path_srarch, false);
                Path dst3 = new Path();
                mMeasure.getSegment(mMeasure.getLength() * mAnimatorValue, mMeasure.getLength(), dst3, true);
                canvas.drawPath(dst3, mPaint);
                break;
        }
    }
}